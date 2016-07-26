/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper.entity;

/**
 *
 * @author murdock
 */
public interface IEvolvable<T> {
    public void evolve(long nanoTimestep);
    public T deepClone();
    public default T spoofEvolve(long nanoTimestep) {
        T newObject = deepClone();
        ((IEvolvable<T>)newObject).evolve(nanoTimestep);
        return newObject;
    }
}
